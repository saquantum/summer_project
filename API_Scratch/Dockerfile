# ---------- deps stage ----------
FROM python:3.12-slim AS deps
ARG POETRY_VERSION=1.8.2
RUN pip install --no-cache-dir "poetry==$POETRY_VERSION"
WORKDIR /app
COPY pyproject.toml poetry.lock ./
RUN poetry install --no-interaction --only main

# ---------- runtime stage ----------
FROM python:3.12-slim
ENV PYTHONUNBUFFERED=1
WORKDIR /app

# 1) 複製已安裝好的系統階層 Python 依賴
COPY --from=deps /usr/local /usr/local


# 2) 複製你的程式碼
#    這裡把整個 package 放進來，以及 CLI 腳本
# COPY src ./src
COPY src/Weather_Location_API_Data ./Weather_Location_API_Data
COPY src/metoffice_poller.py \
     Weather_Location_API_Data/metoffice_poller.py


# 3) 複製 schema（先確保根目錄有 schema.json）
COPY schema.json ./

# 4) 正確的啟動指令：用 python -m 執行該 module
# CMD ["poetry","run","python", \
#      "-m","Weather_Location_API_Data.metoffice_poller", \
CMD ["python","-m","Weather_Location_API_Data.metoffice_poller", \
     "--schema","/app/schema.json", \
     "--output-dir","/data"]
