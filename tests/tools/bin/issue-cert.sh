#!/bin/bash
pki nss-cert-issue \
  $3 \
  --csr $1.csr \
  --ext /usr/share/pki/server/certs/$2.conf \
  --cert $1.crt
openssl x509 -text -noout -in $1.crt
