package handler

import (
	"auth-service/internal/jwt"
	"auth-service/internal/logger"
	"auth-service/internal/password"
	"database/sql"
	"encoding/json"
	"errors"
	"io"
	"net/http"

	"github.com/google/uuid"
)

func (r AuthRouter) Login(w http.ResponseWriter, req *http.Request) {
	rawBody, err := io.ReadAll(req.Body)
	if err != nil {
		r.log.Error("read body error", logger.Err(err))
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	defer req.Body.Close()

	if len(rawBody) == 0 {
		w.WriteHeader(http.StatusBadRequest)
		return
	}

	var body LoginRequest
	if err := json.Unmarshal(rawBody, &body); err != nil {
		r.log.Error("unmarshal json error", logger.Err(err))
		w.WriteHeader(http.StatusInternalServerError)
		return
	}

	if !body.IsValid() {
		w.WriteHeader(http.StatusBadRequest)
		return
	}

	selectUserQuery := `SELECT id, password_hash, role FROM "user" WHERE email = $1 AND deleted_at IS NULL;`

	var userID uuid.UUID
	var passwordHash string
	var role string

	err = r.db.QueryRowContext(req.Context(), selectUserQuery, body.Email).Scan(&userID, &passwordHash, &role)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			w.WriteHeader(http.StatusNotFound)
			return
		}

		r.log.Error("select user error", logger.Err(err))
		w.WriteHeader(http.StatusInternalServerError)
		return
	}

	if !password.Compare(body.Password, passwordHash) {
		w.WriteHeader(http.StatusUnauthorized)
		return
	}

	accessToken, err := jwt.GenerateAccessToken(userID, role, r.cfg.JWT.Secret, r.cfg.JWT.AccessDuration)
	if err != nil {
		r.log.Error("generate access token error", logger.Err(err))
		w.WriteHeader(http.StatusInternalServerError)
		return
	}

	refreshToken, err := jwt.GenerateRefreshToken(userID, role, r.cfg.JWT.Secret, r.cfg.JWT.RefreshDuration)
	if err != nil {
		r.log.Error("generate refresh token error", logger.Err(err))
		w.WriteHeader(http.StatusInternalServerError)
		return
	}

	http.SetCookie(w, &http.Cookie{
		Name:     "access_token",
		Value:    accessToken,
		MaxAge:   int(r.cfg.JWT.AccessDuration.Seconds()),
		HttpOnly: true,
		Path:     "/",
	})

	http.SetCookie(w, &http.Cookie{
		Name:     "refresh_token",
		Value:    refreshToken,
		MaxAge:   int(r.cfg.JWT.RefreshDuration.Seconds()),
		HttpOnly: true,
		Path:     "/",
	})

	w.WriteHeader(http.StatusOK)
}
