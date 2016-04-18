echo $$ > %pid_file%;
docker %docker_command% | tee -a ./docker_tasks;
echo '%task_id%' >> %succeedtasks_filepath%;