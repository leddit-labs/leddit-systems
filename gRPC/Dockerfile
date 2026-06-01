FROM golang:1.26.3

WORKDIR /app

COPY app/go.mod ./
RUN go mod tidy

COPY app/ ./

RUN go build -o main .

CMD ["./main"]