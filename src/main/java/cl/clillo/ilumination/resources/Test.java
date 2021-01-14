package cl.clillo.ilumination.resources;

import lombok.Builder;

@Builder
public class Test {

    private String testCode;
    private String testName;
    private int price;

    public Test() {}

    public Test(String testCode, String testName, int price) {
        this.testCode = testCode;
        this.testName = testName;
        this.price = price;
    }

    public String getTestCode() {
        return testCode;
    }

    public String getTestName() {
        return testName;
    }

    public int getPrice() {
        return price;
    }
}
