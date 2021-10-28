#!/bin/bash
pki nss-cert-import \
  --cert new_sslserver.crt \
  new_sslserver

# verify trust flags
certutil -L -d /root/.dogtag/nssdb
certutil -L -d /root/.dogtag/nssdb | sed -n 's/^new_sslserver\s*\(\S\+\)\s*$/\1/p' > actual
echo "u,u,u" > expected
diff actual expected

# verify key type
certutil -K -d /root/.dogtag/nssdb
certutil -K -d /root/.dogtag/nssdb | sed -n 's/^<.*>\s\+\(\S\+\)\s\+\S\+\s\+NSS Certificate DB:new_sslserver$/\1/p' > actual
echo $1 > expected
diff actual expected

# verify key ID
certutil -K -d /root/.dogtag/nssdb | sed -n 's/^<.*>\s\+\S\+\s\+\(\S\+\)\s\+NSS Certificate DB:new_sslserver$/\1/p' > new_sslserver_key_id
diff sslserver_key_id new_sslserver_key_id
