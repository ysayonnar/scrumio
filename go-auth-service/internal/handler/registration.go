package handler

import (
	"auth-service/internal/db"
	"auth-service/internal/logger"
	"auth-service/internal/password"
	"encoding/json"
	"errors"
	"io"
	"net/http"
	"strings"
	"time"

	"github.com/goombaio/namegenerator"
	"github.com/lib/pq"
)

type LoginRequest struct {
	Email    string `json:"email"`
	Password string `json:"password"`
}

func (r LoginRequest) IsValid() bool {
	return !(len(r.Password) < 8 || !strings.Contains(r.Email, "@"))
}

func (r AuthRouter) Registration(w http.ResponseWriter, req *http.Request) {
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

	passwordHash, err := password.Hash(body.Password)
	if err != nil {
		r.log.Error("hash password error", logger.Err(err))
		w.WriteHeader(http.StatusInternalServerError)
		return
	}

	seed := time.Now().UTC().UnixNano()
	gen := namegenerator.NewNameGenerator(seed)
	name := gen.Generate()

	query := `INSERT INTO "user"(id, name, email, password_hash, role) VALUES (gen_random_uuid(), $1, $2, $3, $4);`

	_, err = r.db.ExecContext(req.Context(), query, name, body.Email, passwordHash, db.USER_ROLE_MEMBER)
	if err != nil {
		var pgErr *pq.Error
		if errors.As(err, &pgErr) {
			if pgErr.Code == "23505" {
				w.WriteHeader(http.StatusConflict)
				return
			}
		}

		r.log.Error("insert user error", logger.Err(err))
		w.WriteHeader(http.StatusInternalServerError)
		return
	}

	w.WriteHeader(http.StatusCreated)
}
