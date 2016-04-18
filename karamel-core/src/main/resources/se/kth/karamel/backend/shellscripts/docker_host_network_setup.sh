echo $$ > %pid_file%;
docker run -d -p 8500:8500 -h consul progrium/consul -server -bootstrap >> ./docker_tasks;
while ! (curl --output /dev/null --silent --head --fail http://localhost:8500/v1/status/peers) ;do sleep 1;done;
sleep 5;
docker network create -d overlay --subnet 10.0.4.0/24 karamel | tee -a ./docker_tasks
docker network inspect karamel | tee -a ./docker_tasks
sleep 5;
echo '%task_id%' >> %succeedtasks_filepath%
