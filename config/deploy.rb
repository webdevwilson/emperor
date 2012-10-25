set :application, "emperor"

set :repository,  "target"
set :scm, :none
set :deploy_via, :copy

role :web, "app1.royalends.com"

ssh_options[:keys] = [File.join(ENV["HOME"], "corykey.pem")]

set :deploy_to, "/home/ubuntu"
set :user, "ubuntu"
set :use_sudo, false
set :copy_exclude, ["streams", "scala*"]

after "deploy:restart", "deploy:cleanup"

namespace :deploy do
  task :start do
    run "nohup current/start -Dconfig.resource=production.conf -Dpidfile.path=/home/ubuntu/emperor.pid >/dev/null 2>&1 &"
  end
  task :stop do
    run "kill -SIGTERM `cat /home/ubuntu/emperor.pid`"
  end
end