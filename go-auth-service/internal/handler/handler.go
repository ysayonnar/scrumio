package handler

import (
	"auth-service/internal/config"
	"log/slog"
	"net/http"

	"github.com/jmoiron/sqlx"
	"github.com/redis/go-redis/v9"
)

type AuthRouter struct {
	log *slog.Logger
	cfg *config.Config
	db  *sqlx.DB
	rdb *redis.Client
}

func NewAuthRouter(cfg *config.Config, log *slog.Logger, db *sqlx.DB, rdb *redis.Client) *AuthRouter {
	return &AuthRouter{
		log: log,
		db:  db,
		cfg: cfg,
		rdb: rdb,
	}
}

func (r AuthRouter) RegisterRoutes(mux *http.ServeMux) {
	mux.HandleFunc("GET /health", func(w http.ResponseWriter, r *http.Request) {})

	mux.HandleFunc("POST /registration", r.Registration)
	mux.HandleFunc("POST /login", r.Login)
	mux.HandleFunc("GET /auth", r.Authenticate)
	mux.HandleFunc("GET /logout", r.Logout)
}
