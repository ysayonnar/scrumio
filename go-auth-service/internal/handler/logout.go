package handler

import (
	"auth-service/internal/jwt"
	"auth-service/internal/logger"
	"errors"
	"net/http"
	"time"

	jwtlib "github.com/golang-jwt/jwt/v5"
)

func (r AuthRouter) Logout(w http.ResponseWriter, req *http.Request) {
	ctx := req.Context()

	accessCookie, err := req.Cookie("access_token")
	if err != nil && !errors.Is(err, http.ErrNoCookie) {
		w.WriteHeader(http.StatusInternalServerError)
		return
	}

	if err == nil {
		accessTokenStr := accessCookie.Value
		claims, err := jwt.ParseToken(accessTokenStr, r.cfg.JWT.Secret)
		if err == nil || errors.Is(err, jwtlib.ErrTokenExpired) {
			if ttl := time.Until(claims.ExpiresAt.Time); ttl > 0 {
				if err := r.rdb.Set(ctx, blacklistPrefix+accessTokenStr, 1, ttl).Err(); err != nil {
					r.log.Error("blacklist access token error", logger.Err(err))
					w.WriteHeader(http.StatusInternalServerError)
					return
				}
			}
		}
		http.SetCookie(w, &http.Cookie{
			Name:     "access_token",
			Value:    "",
			MaxAge:   -1,
			HttpOnly: true,
			Path:     "/",
		})
	}

	refreshCookie, err := req.Cookie("refresh_token")
	if err != nil && !errors.Is(err, http.ErrNoCookie) {
		w.WriteHeader(http.StatusInternalServerError)
		return
	}

	if err == nil {
		refreshTokenStr := refreshCookie.Value
		claims, err := jwt.ParseToken(refreshTokenStr, r.cfg.JWT.Secret)
		if err == nil || errors.Is(err, jwtlib.ErrTokenExpired) {
			if ttl := time.Until(claims.ExpiresAt.Time); ttl > 0 {
				if err := r.rdb.Set(ctx, blacklistPrefix+refreshTokenStr, 1, ttl).Err(); err != nil {
					r.log.Error("blacklist refresh token error", logger.Err(err))
					w.WriteHeader(http.StatusInternalServerError)
					return
				}
			}
		}
		http.SetCookie(w, &http.Cookie{
			Name:     "refresh_token",
			Value:    "",
			MaxAge:   -1,
			HttpOnly: true,
			Path:     "/",
		})
	}

	w.WriteHeader(http.StatusOK)
}
