#!/bin/sh
# Resolve container hostnames and substitute into nginx.conf

IDP_IP=$(getent hosts idp | awk '{ print $1 }' | head -1)
FINANCE_IP=$(getent hosts finance-data-service | awk '{ print $1 }' | head -1)

# If resolution fails, use the container names (nginx will try to resolve them)
: "${IDP_IP:=idp}"
: "${FINANCE_IP:=finance-data-service}"

# Create nginx config with resolved IPs
cat > /etc/nginx/conf.d/default.conf << EOF
resolver 127.0.0.11 valid=1s ipv6=off;
resolver_timeout 2s;

server {
  listen 80;
  server_name _;

  root /usr/share/nginx/html;
  index index.html;

  location /api/ {
    proxy_pass http://${IDP_IP}:8081/api/;
    proxy_set_header Host \$host;
    proxy_set_header X-Real-IP \$remote_addr;
    proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto \$scheme;
  }

  location /finance-api/ {
    proxy_pass http://${FINANCE_IP}:8082/;
    proxy_set_header Host \$host;
    proxy_set_header X-Real-IP \$remote_addr;
    proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto \$scheme;
  }

  location / {
    try_files \$uri \$uri/ /index.html;
  }
}
EOF

# Start nginx
exec nginx -g "daemon off;"
