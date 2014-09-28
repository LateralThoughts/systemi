from fabric.api import *


def deploy(artifact):
    TEMP_DEPLOY_PATH = '/tmp/invoice.lateralthoughts.com.zip'

    # push artifact
    put(artifact, TEMP_DEPLOY_PATH)

    # stop service via supervisord
    run('supervisorctl stop invoice_play')

    # destroy old old version/save old version
    with cd('/opt/invoice.lateral-thoughts.com/'):
        run('tar -cf /tmp/old.invoice.lateral-thoughts.com.tar *')
        run('rm -rf *')
        # unzip l'artefact
        run('tar -xvf %s' % TEMP_DEPLOY_PATH)
        # re-set les droits
        run('chown -R systemi .')

    # restart service supervisord
    run('supervisorctl start invoice_play')

    # check running/status supervisord
    # TODO
    # reload nginx