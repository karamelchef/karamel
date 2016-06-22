echo $$ > %pid_file%;
sudo apt-key adv --keyserver hkp://p80.pool.sks-keyservers.net:80 --recv-keys 58118E89F3A912897C070ADBF76221572C52609D;
sudo sh -c 'echo "deb https://apt.dockerproject.org/repo ubuntu-wily main" > /etc/apt/sources.list.d/docker.list';
sudo apt-get update;
sudo apt-get -y install linux-image-extra-$(uname -r);
sudo apt-get -y install docker-engine;
sudo usermod -a -G docker $USER;
sudo rm -rf /etc/systemd/system/docker.service.d/;
sudo mkdir -p /etc/systemd/system/docker.service.d/;
sudo mkdir /var/repository
echo "[Service]"  | sudo tee --append /etc/systemd/system/docker.service.d/docker.conf;
echo "ExecStart="  | sudo tee --append /etc/systemd/system/docker.service.d/docker.conf;
echo "ExecStart=/usr/bin/docker daemon -H fd:// -g /mnt --cluster-store=consul://%kv_store_ip%:8500 --cluster-advertise=%host_ip%:2375 -H=tcp://0.0.0.0:2375 -H=unix:///var/run/docker.sock" | sudo tee --append /etc/systemd/system/docker.service.d/docker.conf;
sudo systemctl daemon-reload;
sudo systemctl restart docker;
sudo systemctl status docker;
docker info
docker stop $(docker ps -a -q)
docker rm $(docker ps -a -q)
echo '%task_id%' >> %succeedtasks_filepath%
