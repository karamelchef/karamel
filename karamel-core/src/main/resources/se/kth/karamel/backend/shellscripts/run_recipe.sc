echo $$ > %pid_file%; echo $(date '+%H:%M:%S'): '%json_file_name%' >> order && cat > %json_file_name%.json <<-'END_OF_FILE'
%chef_json%
END_OF_FILE && (%sudo_command% chef-solo -c solo.rb -j %json_file_name%.json 2>&1 | tee %log_file_name%.log ; test ${PIPESTATUS[0]} -eq 0 && echo %task_id% >> %succeedtasks_filepath% ; test ${PIPESTATUS[0]} -eq 0)