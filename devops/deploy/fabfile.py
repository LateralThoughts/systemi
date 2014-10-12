from fabric.api import *


def deploy(artifact):
    TEMP_DEPLOY_PATH = '/tmp/invoice.lateralthoughts.com.tar'

    # push artifact
    put(artifact, TEMP_DEPLOY_PATH)
    sudo("chmod 770 %s" % TEMP_DEPLOY_PATH)

    # stop service via supervisord
    sudo('supervisorctl stop invoice_play')

    # destroy old old version/save old version
    with cd('/opt/invoice.lateral-thoughts.com/'):
        run('tar -cf /tmp/old.invoice.lateral-thoughts.com.tar *')
        sudo('rm -rf *')
        # unzip l'artefact
        run('tar -xvf %s' % TEMP_DEPLOY_PATH)
        # re-set les droits
        sudo('chown -R systemi:deployer .')
        sudo('chmod -R 770 .')
        run('rm -rf %s' % TEMP_DEPLOY_PATH)

    # restart service supervisord
    sudo('supervisorctl start invoice_play')

    # check running/status supervisord
    # TODO
    # reload nginx