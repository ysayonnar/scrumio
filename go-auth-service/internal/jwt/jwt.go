package jwt

import (
	"time"

	"github.com/golang-jwt/jwt/v5"
	"github.com/google/uuid"
)

type Claims struct {
	Role string `json:"role"`
	jwt.RegisteredClaims
}

func GenerateAccessToken(userID uuid.UUID, role string, secret string, duration time.Duration) (string, error) {
	return generate(userID, role, secret, duration)
}

func GenerateRefreshToken(userID uuid.UUID, role string, secret string, duration time.Duration) (string, error) {
	return generate(userID, role, secret, duration)
}

func generate(userID uuid.UUID, role string, secret string, duration time.Duration) (string, error) {
	claims := Claims{
		Role: role,
		RegisteredClaims: jwt.RegisteredClaims{
			Subject:   userID.String(),
			IssuedAt:  jwt.NewNumericDate(time.Now()),
			ExpiresAt: jwt.NewNumericDate(time.Now().Add(duration)),
		},
	}
	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	return token.SignedString([]byte(secret))
}
