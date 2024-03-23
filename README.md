# TMoe


## 本仓库复刻于原作者的仓库（https://github.com/cinit/TMoe）
## 感谢原作者以及参与构建的构建者


[![license](https://img.shields.io/github/license/cinit/TMoe.svg)](https://www.gnu.org/licenses/gpl-3.0.html)
[![GitHub release](https://img.shields.io/github/release/cinit/TMoe.svg)](https://github.com/cinit/TMoe/releases/latest)
[![Telegram](https://img.shields.io/static/v1?label=Telegram&message=TMoe0&color=0088cc)](https://t.me/TMoe0)

TMoe 是一个兼容若干第三方开源 Telegram 客户端的开源 Xposed 模块

TMoe is an open source Xposed module compatible with several third-party open source Telegram clients.

## 使用方法 / Usage

激活本模块后，在 Telegram 客户端的设置中点击 "TMoe 设置" 即可开关对应功能。

After activating this module, click "TMoe Settings" in the Telegram client settings to turn on or off the corresponding function.

## 一切开发旨在学习，请勿用于非法用途

- 本项目保证永久开源，欢迎提交 Issue 或者 Pull Request，但是请不要提交用于非法用途的功能。
- 如果某功能被大量运用于非法用途，那么该功能将会被移除。
- 开发人员可能在任何时间**停止更新**或**删除项目**

## For educational purposes only, do not use for illegal purposes

- This project is guaranteed to be open source forever. Welcome to submit issues or pull requests, but do not submit features for illegal purposes.
- If a feature is widely used for illegal purposes, the feature will be removed.
- The developer may **stop updating** or **delete the project** at any time.

## 功能介绍 / Features

1. 调试模式 / Debug mode
2. 去除复制保存消息限制 / Remove the limit on copying and saving messages
3. 隐藏赞助消息 / Hide sponsored message
4. 隐藏指定用户头像 / Hide specific user avatars
5. 禁用剧透 / Prohibit Spoilers
6. 禁止频道滑动切换 / Prohibit Channel Switching
7. 禁止表情回应 / Prohibit Enable Reactions
8. 禁用问候贴纸 / Prohibit Chat Greetings
9. 隐藏电话号码 / Hide Phone Number
10. 拒绝人数舍入 / Show Channel Detail Numbers
11. 允许非管理员查询部分群组信息 / Allow non-admin to query some group information
12. 询问是否发出指令 / Ask for tap command
13. 强制显示顶栏模糊选项 / Force chat blur option available
14. 禁用会员贴纸动画 / Disable Premium Sticker Animation
15. 按音量键时保持视频静音 / Keep video muted when pressing the volume button
16. 以消息模式查看话题群组 / View as Group by default in Topic
17. 隐藏标签面板的会员贴纸包 / Hide Premium Sticker Tab
18. 显示消息ID / Show message ID
19. 为聊天菜单添加刷新消息按钮 / Add Reload Message Button in chat menu
20. TODO 咕咕咕

## 支持的客户端 / Supported clients

- 任何基于 Telegram Android 官方 [TMessagesProj](https://github.com/DrKLO/Telegram) 的无混淆客户端。

  Any official Telegram Android [TMessagesProj](https://github.com/DrKLO/Telegram) based client without obfuscation.

- 完整的列表请参考 [HookEntry.java](app/src/main/java/cc/ioctl/tmoe/startup/HookEntry.java)
  以及 [模块作用域](app/src/main/res/values/arrays.xml).

  Please refer to [HookEntry.java](app/src/main/java/cc/ioctl/tmoe/startup/HookEntry.java)
  and [Xposed scope](app/src/main/res/values/arrays.xml) for the complete list.

- 如果您的客户端满足兼容性要求但不在列表中，请在 [HookEntry.java](app/src/main/java/cc/ioctl/tmoe/startup/HookEntry.java)
  和 [模块作用域](app/src/main/res/values/arrays.xml) 中添加对应的值。

  If your client meets the compatibility requirements but is not in the list, please add the corresponding value
  in [HookEntry.java](app/src/main/java/cc/ioctl/tmoe/startup/HookEntry.java) and [Xposed scope](app/src/main/res/values/arrays.xml).

## License

- [GPL-3.0](https://www.gnu.org/licenses/gpl-3.0.html)

```
Copyright (C) 2021-2022 xenonhydride@gmail.com

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
```
