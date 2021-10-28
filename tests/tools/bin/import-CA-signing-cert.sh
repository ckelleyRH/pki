#!/bin/bash
pki nss-cert-import \
  --cert ca_signing.crt \
  --trust CT,C,C \
  ca_signing

# verify trust flags
certutil -L -d /root/.dogtag/nssdb
certutil -L -d /root/.dogtag/nssdb | sed -n 's/^ca_signing\s*\(\S\+\)\s*$/\1/p' > actual
echo "CTu,Cu,Cu" > expected
diff actual expected

# verify key type
certutil -K -d /root/.dogtag/nssdb
certutil -K -d /root/.dogtag/nssdb | sed -n 's/^<.*>\s\+\(\S\+\)\s\+\S\+\s\+NSS Certificate DB:ca_signing$/\1/p' > actual
echo $1 > expected
diff actual expected
