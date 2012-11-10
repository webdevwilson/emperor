require 'rubygems'
require 'railsless-deploy'

set :application, "emperor"

set :scm, :none
set :repository,  "target"
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
    run "nohup current/start -Dconfig.file=/home/ubuntu/production.conf -Dpidfile.path=/home/ubuntu/emperor.pid -DapplyEvolutions.default=true >/dev/null 2>&1 &"
  end
  task :stop do
    run "kill -15 `cat /home/ubuntu/emperor.pid`"
  end
end
