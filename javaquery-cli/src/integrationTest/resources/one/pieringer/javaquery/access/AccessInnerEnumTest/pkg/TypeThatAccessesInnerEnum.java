package pkg;

class TypeThatAccessesInnerEnum {

    void method() {
        OuterClass.InnerEnumType typeOfField = OuterClass.InnerEnumType.ONE;
    }
}
