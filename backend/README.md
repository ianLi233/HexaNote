on windows install nexa cli ARM64 and run
```
 nexa pull NexaAI/jina-v2-rerank-npu
 nexa pull  NexaAI/Llama3.2-3B-NPU-Turbo 

nexa serve --host 0.0.0.0:8883 --keepalive 60000
 ```

 Make sure the python version is py3.11-3.13 and is of ARM64 distribution for optimal performance



 then in WSL make sure you have docker installed.

```bash
# in WSL
export WINDOWS_HOST=$(ip route | grep default | awk '{print $3}')         # get the windows host IP

echo $WINDOWS_HOST
#172.23.224.1, modify in backend code if needed.

cd ..

docker compose up -d --build backend



docker logs hexanote-backend -f # this to check logs
```


