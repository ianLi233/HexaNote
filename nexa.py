import time 
from nexaai import LLM, ModelConfig

llm = LLM(from_config=ModelConfig(
    model_name="NexaAI/Llama-2-7b-chat-h",

for t in llm.chat("What is the capital of France?"):
    print(t, end="")