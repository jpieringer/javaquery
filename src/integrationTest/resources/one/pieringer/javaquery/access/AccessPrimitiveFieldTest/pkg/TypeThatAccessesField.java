package pkg;

class TypeThatAccessesField {

    void method() {
        TypeWithField typeWithField;
        int typeOfField = typeWithField.field;
    }
}
