echo "Waiting for ACME server to be ready"
start_time=$(date +%s)
MAX_WAIT=60

while :
do
    sleep 1

    response=$(curl --write-out '%{http_code}' --silent --output /dev/null http://pki.example.com:8080/acme/directory)
    echo ${response}
    if [ ${response} == "200" ]
    then
        break
    fi

    current_time=$(date +%s)
    elapsed_time=$(expr $current_time - $start_time)

    if [ $elapsed_time -ge $MAX_WAIT ]
    then
        echo "ACME server not ready after ${MAX_WAIT}s"
        exit 1
    fi

    echo "Waiting for ACME server to start (${elapsed_time}s)"
done

echo "ACME server is started"
