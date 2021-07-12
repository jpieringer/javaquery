package pkg;

class TypeThatAccessesField {

    void method() {
        TypeWithField typeWithField;
        EnumTypeOfField typeOfField = typeWithField.field;
    }
}
