package handler

import (
	"auth-service/internal/jwt"
	"auth-service/internal/logger"
	"encoding/json"
	"errors"
	"net/http"
	"time"

	jwtlib "github.com/golang-jwt/jwt/v5"
	"github.com/google/uuid"
)

const blacklistPrefix = "blacklist:"

type AuthResponse struct {
	UserID string `json:"user_id"`
	Role   string `json:"role"`
}

func writeAuthResponse(w http.ResponseWriter, claims *jwt.Claims) {
	resp, _ := json.Marshal(AuthResponse{
		UserID: claims.Subject,
		Role:   claims.Role,
	})
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	w.Write(resp)
}

func (r AuthRouter) Authenticate(w http.ResponseWriter, req *http.Request) {
	ctx := req.Context()

	accessCookie, err := req.Cookie("access_token")
	if err != nil && !errors.Is(err, http.ErrNoCookie) {
		w.WriteHeader(http.StatusInternalServerError)
		return
	}

	var accessTokenStr string
	var accessClaims *jwt.Claims

	if err == nil {
		accessTokenStr = accessCookie.Value

		blacklisted, err := r.rdb.Exists(ctx, blacklistPrefix+accessTokenStr).Result()
		if err != nil {
			r.log.Error("redis exists error", logger.Err(err))
			w.WriteHeader(http.StatusInternalServerError)
			return
		}
		if blacklisted > 0 {
			r.log.Debug("access token is in blacklist")
			w.WriteHeader(http.StatusUnauthorized)
			return
		}

		accessClaims, err = jwt.ParseToken(accessTokenStr, r.cfg.JWT.Secret)
		if err != nil && !errors.Is(err, jwtlib.ErrTokenExpired) {
			r.log.Debug("access token is expired")
			w.WriteHeader(http.StatusUnauthorized)
			return
		}

		// Access token is valid → return user info immediately
		if err == nil {
			writeAuthResponse(w, accessClaims)
			return
		}
		// err == ErrTokenExpired → fall through to refresh flow
	}

	refreshCookie, err := req.Cookie("refresh_token")
	if err != nil {
		r.log.Debug("no refresh_token cookie found")
		w.WriteHeader(http.StatusUnauthorized)
		return
	}
	refreshTokenStr := refreshCookie.Value

	blacklisted, err := r.rdb.Exists(ctx, blacklistPrefix+refreshTokenStr).Result()
	if err != nil {
		r.log.Error("redis exists error", logger.Err(err))
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	if blacklisted > 0 {
		r.log.Debug("refresh token is in blacklist")
		w.WriteHeader(http.StatusUnauthorized)
		return
	}

	refreshClaims, err := jwt.ParseToken(refreshTokenStr, r.cfg.JWT.Secret)
	if err != nil {
		r.log.Debug("refresh token expired")
		w.WriteHeader(http.StatusUnauthorized)
		return
	}

	if accessTokenStr != "" && accessClaims != nil {
		if ttl := time.Until(accessClaims.ExpiresAt.Time); ttl > 0 {
			if err := r.rdb.Set(ctx, blacklistPrefix+accessTokenStr, 1, ttl).Err(); err != nil {
				r.log.Error("blacklist access token error", logger.Err(err))
				w.WriteHeader(http.StatusInternalServerError)
				return
			}
		}
	}

	if ttl := time.Until(refreshClaims.ExpiresAt.Time); ttl > 0 {
		if err := r.rdb.Set(ctx, blacklistPrefix+refreshTokenStr, 1, ttl).Err(); err != nil {
			r.log.Error("blacklist refresh token error", logger.Err(err))
			w.WriteHeader(http.StatusInternalServerError)
			return
		}
	}

	userID, err := uuid.Parse(refreshClaims.Subject)
	if err != nil {
		r.log.Error("parse user id from claims error", logger.Err(err))
		w.WriteHeader(http.StatusInternalServerError)
		return
	}

	newAccessToken, err := jwt.GenerateAccessToken(userID, refreshClaims.Role, r.cfg.JWT.Secret, r.cfg.JWT.AccessDuration)
	if err != nil {
		r.log.Error("generate access token error", logger.Err(err))
		w.WriteHeader(http.StatusInternalServerError)
		return
	}

	newRefreshToken, err := jwt.GenerateRefreshToken(userID, refreshClaims.Role, r.cfg.JWT.Secret, r.cfg.JWT.RefreshDuration)
	if err != nil {
		r.log.Error("generate refresh token error", logger.Err(err))
		w.WriteHeader(http.StatusInternalServerError)
		return
	}

	http.SetCookie(w, &http.Cookie{
		Name:     "access_token",
		Value:    newAccessToken,
		MaxAge:   int(r.cfg.JWT.AccessDuration.Seconds()),
		HttpOnly: true,
		Path:     "/",
	})

	http.SetCookie(w, &http.Cookie{
		Name:     "refresh_token",
		Value:    newRefreshToken,
		MaxAge:   int(r.cfg.JWT.RefreshDuration.Seconds()),
		HttpOnly: true,
		Path:     "/",
	})

	writeAuthResponse(w, refreshClaims)
}
