import dev.langchain4j.model.openai.OpenAiChatModel;

public class Main {
    public static void main(String[] args) {
        String apiKey = System.getenv("OPENAI_API_KEY");

        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gpt-4.1-nano") //most coste efficient: gpt-4.1-nano https://platform.openai.com/docs/models/gpt-4.1-nano; still pretty cost efficient: gpt-4o-mini https://platform.openai.com/docs/models/gpt-4o-mini
                .temperature(0.0) //todo fancy als params machen
                .build();

        String answer = model.chat("Say 'Hi'"); //todo prompt (inkl. rolle und output format)
        System.out.println(answer);

        //TODO use https://docs.langchain4j.dev/tutorials/structured-outputs/ to generate Java Class from output
    }
}
