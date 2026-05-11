import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

// ============================================================
// OBSERVER PATTERN
// ============================================================
interface MessageListener {
    void onMessagePrinted(String message);
}

class LoggingListener implements MessageListener {
    @Override
    public void onMessagePrinted(String message) {
        System.out.println("[LOG] Message dispatched: \"" + message + "\"");
    }
}

class AuditListener implements MessageListener {
    @Override
    public void onMessagePrinted(String message) {
        System.out.println("[AUDIT] Character count: " + message.length());
    }
}

// ============================================================
// SINGLETON — MessageBroker
// ============================================================
class MessageBroker {
    private static MessageBroker instance;
    private final List<MessageListener> listeners = new ArrayList<>();

    private MessageBroker() {}

    public static MessageBroker getInstance() {
        if (instance == null) {
            instance = new MessageBroker();
        }
        return instance;
    }

    public void subscribe(MessageListener listener) {
        listeners.add(listener);
    }

    public void publish(String message) {
        for (MessageListener l : listeners) {
            l.onMessagePrinted(message);
        }
    }
}

// ============================================================
// BUILDER PATTERN — MessageBuilder
// ============================================================
class Message {
    private final String greeting;
    private final String target;
    private final String punctuation;

    private Message(Builder builder) {
        this.greeting    = builder.greeting;
        this.target      = builder.target;
        this.punctuation = builder.punctuation;
    }

    public String build() {
        return greeting + ", " + target + punctuation;
    }

    public static class Builder {
        private String greeting    = "Hello";
        private String target      = "World";
        private String punctuation = "!";

        public Builder greeting(String greeting) {
            this.greeting = greeting;
            return this;
        }

        public Builder target(String target) {
            this.target = target;
            return this;
        }

        public Builder punctuation(String punctuation) {
            this.punctuation = punctuation;
            return this;
        }

        public Message build() {
            return new Message(this);
        }
    }
}

// ============================================================
// CHAIN OF RESPONSIBILITY — String pipeline
// ============================================================
abstract class StringHandler {
    protected StringHandler next;

    public StringHandler setNext(StringHandler next) {
        this.next = next;
        return next;
    }

    public abstract String handle(String input);
}

class TrimHandler extends StringHandler {
    @Override
    public String handle(String input) {
        String result = input.trim();
        return next != null ? next.handle(result) : result;
    }
}

class ExclamationEnsureHandler extends StringHandler {
    @Override
    public String handle(String input) {
        String result = input.endsWith("!") ? input : input + "!";
        return next != null ? next.handle(result) : result;
    }
}

class UpperFirstHandler extends StringHandler {
    @Override
    public String handle(String input) {
        String result = input.isEmpty() ? input
                : Character.toUpperCase(input.charAt(0)) + input.substring(1);
        return next != null ? next.handle(result) : result;
    }
}

// ============================================================
// STRATEGY PATTERN — Print strategies
// ============================================================
interface PrintStrategy {
    void print(String message);
}

class ConsolePrintStrategy implements PrintStrategy {
    @Override
    public void print(String message) {
        System.out.println("[CONSOLE] " + message);
    }
}

class UpperCasePrintStrategy implements PrintStrategy {
    @Override
    public void print(String message) {
        System.out.println("[UPPER]   " + message.toUpperCase());
    }
}

class ReversePrintStrategy implements PrintStrategy {
    @Override
    public void print(String message) {
        System.out.println("[REVERSE] " + new StringBuilder(message).reverse());
    }
}

// ============================================================
// FACTORY METHOD — PrinterFactory
// ============================================================
enum PrinterType { CONSOLE, UPPER, REVERSE }

class PrinterFactory {
    public static PrintStrategy create(PrinterType type) {
        return switch (type) {
            case CONSOLE -> new ConsolePrintStrategy();
            case UPPER   -> new UpperCasePrintStrategy();
            case REVERSE -> new ReversePrintStrategy();
        };
    }
}

// ============================================================
// MAIN
// ============================================================
public class HelloWorld {
    public static void main(String[] args) {

        // 1. Build message via Builder
        String rawMessage = new Message.Builder()
                .greeting("Hello")
                .target("World")
                .punctuation("!")
                .build()
                .build();

        // 2. Run through Chain of Responsibility pipeline
        StringHandler pipeline = new TrimHandler();
        pipeline.setNext(new ExclamationEnsureHandler())
                .setNext(new UpperFirstHandler());
        String message = pipeline.handle(rawMessage);

        // 3. Register observers
        MessageBroker broker = MessageBroker.getInstance();
        broker.subscribe(new LoggingListener());
        broker.subscribe(new AuditListener());

        // 4. Print using multiple strategies via Factory
        System.out.println("=== Hello World (Over-Engineered Edition) ===");
        for (PrinterType type : PrinterType.values()) {
            PrintStrategy strategy = PrinterFactory.create(type);
            strategy.print(message);
            broker.publish(message);
            System.out.println();
        }
    }
}
