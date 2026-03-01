package handler

import (
	"auth-service/internal/config"
	"log/slog"
	"net/http"

	"github.com/jmoiron/sqlx"
)

type AuthService interface {
}

type AuthRouter struct {
	log *slog.Logger
	cfg *config.Config
	db  *sqlx.DB
}

func NewAuthRouter(cfg *config.Config, log *slog.Logger, db *sqlx.DB) *AuthRouter {
	return &AuthRouter{log: log, db: db, cfg: cfg}
}

func (r AuthRouter) RegisterRoutes(mux *http.ServeMux) {
	mux.HandleFunc("GET /health", func(w http.ResponseWriter, r *http.Request) {})
	//mux.HandleFunc("POST /carts", h.createCart)
	//mux.HandleFunc("POST /carts/{cart_id}/items", h.addToCart)
	//mux.HandleFunc("DELETE /carts/{cart_id}/items/{item_id}", h.removeFromCart)
	//mux.HandleFunc("GET /carts/{cart_id}", h.viewCart)
	//mux.HandleFunc("GET /carts/{cart_id}/price", h.calculateCartPrice)
}
