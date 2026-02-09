git remote add -f integrationTests git@github.com:ardoco/IntegrationTests.git
git fetch integrationTests main
git subtree pull --prefix tests/integration-tests integrationTests main --squash

read -n 1 -s -p "Press any key to exit..."
