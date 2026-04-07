package minijava.compiler.ast;

/**
 * Representa um tipo em MiniJava.
 */
public class Type extends ASTNode {
    public enum BaseType {
        INT, BOOLEAN, STRING, OBJECT
    }

    public final BaseType baseType;
    public final String className;  // não null apenas se baseType == OBJECT
    public final boolean isArray;

    public Type(BaseType baseType, String className, boolean isArray) {
        this.baseType = baseType;
        this.className = className;
        this.isArray = isArray;
    }

    // Construtores auxiliares
    public Type(BaseType baseType, boolean isArray) {
        this(baseType, null, isArray);
    }

    public Type(BaseType baseType) {
        this(baseType, null, false);
    }

    public Type(String className, boolean isArray) {
        this(BaseType.OBJECT, className, isArray);
    }

    public Type(String className) {
        this(BaseType.OBJECT, className, false);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitType(this);
    }

    @Override
    public String toString() {
        String typeName = baseType == BaseType.OBJECT ? className : baseType.toString().toLowerCase();
        return isArray ? typeName + "[]" : typeName;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Type)) return false;
        Type other = (Type) obj;
        return baseType == other.baseType && 
               (className == null ? other.className == null : className.equals(other.className)) &&
               isArray == other.isArray;
    }

    @Override
    public int hashCode() {
        int result = baseType.hashCode();
        result = 31 * result + (className != null ? className.hashCode() : 0);
        result = 31 * result + (isArray ? 1 : 0);
        return result;
    }
}

