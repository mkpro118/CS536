docker kill cs536p5
docker rm cs536p5
docker build . -t cs536p5
docker run --name cs536p5 -d -v .\tests:/src/tests cs536p5
docker exec -it cs536p5 bash
