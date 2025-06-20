# Summer Project: OurRainWater

[Jira Link](https://amateureconomist.atlassian.net/jira/software/projects/SPWATER/boards/37/backlog?atlOrigin=eyJpIjoiODM2NGQwNTQwNzQzNGQ4YmI4OTg4YmQ3MDdiYTE4YmIiLCJwIjoiaiJ9)
[Overleaf](https://www.overleaf.com/3683468853gzxngvxzczhh#0833f8)

[Our RainWater shared link](https://ourrainwater.sharepoint.com/sites/SuDS_alerts/Shared%20Documents/Forms/AllItems.aspx?id=%2Fsites%2FSuDS%5Falerts%2FShared%20Documents%2FDevelopment%20resources&viewid=d11e3f4b%2D6334%2D43b8%2D8643%2Dd1fb0390a45f)

## 前置需求
- Python 3.10 以上
- Poetry (1.8+)


# 安裝 Poetry（一次而已）
curl -sSL https://install.python-poetry.org | python -

# 建立 / 重新計算 lockfile
poetry lock

# 其他開發者（other developer）
## 重現環境: poetry install --with dev
make install

## 安裝 pre-commit hook (僅需第一次)
poetry run pip install pre-commit
poetry run pre-commit install


## 驗證
### pytest 全綠
make TEST_PATH=tests/mini_test.py test
### flake8 無警告
make lint

## 啟動主程式（產出 JSON 到 ./data＿
make run