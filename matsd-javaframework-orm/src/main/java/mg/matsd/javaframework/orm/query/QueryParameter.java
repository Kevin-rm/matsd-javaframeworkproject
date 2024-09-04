package mg.matsd.javaframework.orm.query;

import mg.matsd.javaframework.core.annotations.Nullable;
import mg.matsd.javaframework.core.utils.Assert;

class QueryParameter {
    private final RawQuery<?> rawQuery;
    private int    index;
    @Nullable
    private String name;
    @Nullable
    private final Object value;

    QueryParameter(RawQuery<?> rawQuery, String name, @Nullable Object value) {
        this.rawQuery = rawQuery;
        this.value    = value;
        this.setName(name);

        convertNameToPlaceholder();
    }

    QueryParameter(RawQuery<?> rawQuery, int index, @Nullable Object value) {
        this.rawQuery = rawQuery;
        this.value    = value;
        this.setIndex(index);

        convertNameToPlaceholder();
    }

    int getIndex() {
        return index;
    }

    private void setIndex(int index) {
        Assert.positive(index, "L'indice d'un paramètre ne peut pas être négatif ou nul");

        int placeholdersCount = countParameterPlaceholders(rawQuery.getSql());
        if (index > placeholdersCount)
            throw new QueryParameterException(
                String.format("L'indice spécifié (=%d) dépasse le nombre de paramètres dans la requête (=%d)", index, placeholdersCount)
            );

        this.index = index;
    }

    private void setName(String name) {
        String sql = rawQuery.getSql();

        Assert.notBlank(name, false, "Le nom d'un paramètre ne peut pas être vide ou \"null\"");
        Assert.state(!name.contains(":") && !name.contains("?"),
            () -> new QueryParameterException("Le nom d'un paramètre ne peut pas contenir les caractères suivants : \":\" et \"?\"")
        );
        Assert.state(sql.contains(String.format(":%s", name)),
            () -> new QueryParameterException(String.format("Aucun paramètre nommé \"%s\" n'est pas présent dans la requête : \"%s\"", name, sql))
        );

        this.name = name;
    }

    Object getValue() {
        return value;
    }

    private void convertNameToPlaceholder() {
        String sql = rawQuery.getSql();

        String formattedName  = String.format(":%s", name);
        int nameLastWordIndex = sql.indexOf(formattedName) + formattedName.length();

        String sqlSubString  = sql
            .substring(0, nameLastWordIndex).strip()
            .replace  (formattedName, "?");

        index = countParameterPlaceholders(sqlSubString);
        rawQuery.setSql(sqlSubString + sql.substring(nameLastWordIndex));
    }

    private static int countParameterPlaceholders(String sql) {
        int count = 0;
        for (int i = 0; i < sql.length(); i++) if (sql.charAt(i) == '?') count++;

        return count;
    }

    @Override
    public String toString() {
        return "QueryParameter{" +
            "index=" + index +
            ", name='" + name + '\'' +
            ", value=" + value +
            '}';
    }
}
