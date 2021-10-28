#!/bin/bash
pki nss-cert-request \
  $3 \
  $4 \
  --subject "CN=pki.example.com" \
  --ext /usr/share/pki/server/certs/$2.conf \
  --csr $1.csr
openssl req -text -noout -in $1.csr
