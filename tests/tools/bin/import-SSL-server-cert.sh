#!/bin/bash
pki nss-cert-import \
  --cert sslserver.crt \
  sslserver

# verify trust flags
certutil -L -d /root/.dogtag/nssdb
certutil -L -d /root/.dogtag/nssdb | sed -n 's/^sslserver\s*\(\S\+\)\s*$/\1/p' > actual
echo "u,u,u" > expected
diff actual expected

# verify key type
certutil -K -d /root/.dogtag/nssdb
certutil -K -d /root/.dogtag/nssdb | sed -n 's/^<.*>\s\+\(\S\+\)\s\+\S\+\s\+NSS Certificate DB:sslserver$/\1/p' > actual
echo $1 > expected
diff actual expected

# get key ID
certutil -K -d /root/.dogtag/nssdb | sed -n 's/^<.*>\s\+\S\+\s\+\(\S\+\)\s\+NSS Certificate DB:sslserver$/\1/p' > sslserver_key_id
