package pkg;

class TypeThatAccessesField {

    void method() {
        TypeWithField typeWithField;
        TypeOfField typeOfField = typeWithField.field;
    }
}
