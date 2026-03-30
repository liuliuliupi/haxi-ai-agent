# Haxi AI Agent
 
> 基于 Spring AI Alibaba Agent Framework 构建的智能 AI Agent 系统，集成 RAG、MCP、多工具协作等企业级能力。
 
## 📋 项目简介
 
Haxi AI Agent 是一个功能完善的 AI Agent 框架实现，提供了从基础 Agent 执行循环到复杂 RAG 流水线的完整解决方案。项目采用前后端分离架构，后端基于 Spring Boot 3.5.11 和 Spring AI 框架，前端使用 Vue 3 构建。
 
## ✨ 核心特性
 
### Agent 框架
- **BaseAgent 执行循环** - 标准化的 Agent 执行引擎
- **ReAct 思考-行动模式** - 实现推理与动作的协同
- **ToolCallAgent** - 工具调用型 Agent 实现
- **HaxiManus 通用 Agent** - 可扩展的通用 Agent 架构
- **Agent 状态管理** - 完善的状态持久化机制
 
### 工具系统
- **工具注册与生命周期** - 灵活的工具管理机制
- **网页搜索与抓取工具** - 集成 jsoup 进行网页解析
- **文件与 PDF 操作工具** - 基于 iText 的 PDF 生成与处理
- **终止工具与步骤控制** - 智能的执行流程控制
 
### RAG 流水线
- **文档加载器** - 支持 Markdown 等多种格式
- **向量存储配置** - 基于 PostgreSQL + PGVector
- **查询重写策略** - 优化检索质量
- **RAG Advisor 工厂模式** - 灵活的 RAG 链配置
 
### MCP 集成
- **MCP 客户端** - 完整的 Model Context Protocol 支持
- **图像搜索 MCP 服务器** - 独立的图像搜索服务模块
 
### 其他特性
- **基于文件的聊天记忆** - 使用 Kryo 序列化实现持久化
- **自定义 Advisor 链** - 可组合的 Advisor 策略
- **SSE 流式响应** - 实时流式输出支持
- **REST API** - 标准化的 RESTful 接口
- **Knife4j API 文档** - 自动生成的 API 文档
 
## 🛠️ 技术栈
 
### 后端技术
- **框架**: Spring Boot 3.5.11
- **语言**: Java 21
- **AI 框架**: 
  - Spring AI Alibaba Agent Framework 1.1.2.0
  - Spring AI 1.1.2
- **大模型**: 阿里云百炼 (DashScope)
- **LangChain**: Langchain4j 1.11.0-beta19
- **向量存储**: PostgreSQL + PGVector
- **序列化**: Kryo 5.6.2
- **HTML 解析**: Jsoup 1.22.1
- **PDF 处理**: iText 9.5.0
- **工具库**: Hutool 5.8.38
- **API 文档**: Knife4j 4.4.0
- **工具**: Lombok 1.18.36
 
### 前端技术
- **框架**: Vue 3.5.13
- **路由**: Vue Router 4.5.0
- **状态管理**: Pinia 2.3.0
- **HTTP 客户端**: Axios 1.7.9
- **构建工具**: Vite 6.0.5
- **语言**: TypeScript 5.7.2
 
## 📁 项目结构
```text
haxi-ai-agent/
├── 📦 haxi-ai-agent-frontend/     │   ├── views/
│   │   ├── HomeView.vue            #   首页 - 工作台导航入口
│   │   ├── TravelWorkbench.vue     #   旅行规划聊天界面
│   │   └── ManusWorkbench.vue      #   Manus 通用智能体界面
│   ├── utils/
│   │   ├── axios.ts                #   HTTP 请求封装
│   │   └── sse.ts                  #   SSE 连接管理器
│   └── router/index.ts             #   路由配置
│
├── 📦 haxi-image-search-mcp-server/ # MCP 图片搜索服务（独立部署）
│   └── src/main/java/com/haxi/     #   图片搜索工具实现
│
└── 📦 ./                            # 主后端服务
    └── src/main/java/com/haxi/
        ├── agent/                   #   Agent 框架（核心）
        ├── app/                     #   业务应用（TravelApp）
        ├── tools/                   #   工具集
        ├── rag/                     #   RAG 管道
        ├── advisor/                 #   自定义 Advisor
        ├── chatmemory/              #   对话记忆持久化
        ├── controller/              #   REST API 端点
        └── config/                  #   配置类
```
