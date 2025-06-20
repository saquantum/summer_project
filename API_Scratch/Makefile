.PHONY: install lock test lint build run shell

install:          ## 安裝依賴 (含 dev)
	poetry install --with dev

lock:             ## 重新產生 lockfile
	poetry lock --no-update

test:             ## 執行 pytest ## poetry run pytest -q
	poetry run pytest -q $(TEST_PATH)

lint:             ## flake8
	poetry run flake8 src tests

build:            ## 建 dev image
	docker build -t wlapi:dev .

run:              ## 用 dev image 執行 poller
	docker run --rm -v $$(pwd)/data:/data wlapi:dev

shell:            ## 進容器除錯
	docker run --rm -it -v $$(pwd):/app wlapi:dev bash