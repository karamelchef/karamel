echo $$ > %pid_file%;
docker run -d -p 8500:8500 -h consul progrium/consul -server -bootstrap;
sleep 3;
docker network create -d overlay --subnet 10.0.4.0/24 karamel
echo '%task_id%' > %succeedtasks_filepath%
