# Introduction

This folder contains the working demo of the email sending functions and relative html pages.

*Please use your own email address and corresponding password or not send too many emails using my email*

## Steps 1

Example of Gmail

Generate substitute password:http://gogo.wang/blog/%E8%B0%B7%E6%AD%8Cgmail%E9%82%AE%E7%AE%B1%E5%BC%80%E5%90%AFsmtp-imap%E6%9C%8D%E5%8A%A1%E5%8F%8A%E8%8E%B7%E5%8F%96%E6%8E%88%E6%9D%83%E7%A0%81%E6%B5%81%E7%A8%8B

Replace my gmail address and password as comments describe in some files

## Step 2

Run init.sql in PostgreSQL(pgAdmin)

## Step 3

Run MailApplication.java and visit localhost:8080

Before this step, you may still need some pre-work(just like what you need to do before running code in main branch)

# For WhatsApp Part

## First set up test

https://console.twilio.com/us1/develop/sms/try-it-out/whatsapp-learn?frameUrl=%2Fconsole%2Fsms%2Fwhatsapp%2Flearn%3Fx-target-region%3Dus1

You can switch your new twilio setting(in file application.properties)

The phone numbers you send message to should be added into your/my Twilio Sandbox

# For Discord Part

You don't need any pre-set.

*Follow these steps to create a webhook:*

🧩 步骤 1：打开 Discord，并进入你的服务器
在左侧选择你拥有或管理的某个服务器，点击进入。

🧩 步骤 2：选择你想使用的频道（通常是文本频道）
例如：#notifications 或 #general

🧩 步骤 3：点击频道名右侧的齿轮图标（⚙️）打开“编辑频道”
🧩 步骤 4：点击左侧菜单中的 “整合” / Integrations
🧩 步骤 5：点击 “Webhooks”
你会看到已有的 Webhook 列表（如果有），你可以：

查看已有的 Webhook（点击可见 webhook URL）

或点击 “新建 Webhook” / "Create Webhook"

🧩 步骤 6：查看或复制 Webhook URL

Run MailApplication.java and visit localhost:8080
