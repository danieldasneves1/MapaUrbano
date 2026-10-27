package v1.mapaurbano.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.text.Normalizer;
import java.util.Locale;

@Getter
@RequiredArgsConstructor
public enum Categoria {
    BURACO_NA_RUA("Buraco na rua"),
    iLUMINAÇAO_PUBLICA("Iluminaçao publica"),
    LIXO_ACUMULADO("Lixo acumulado"),
    VAZAMENTO_DE_AGUA("Vazamento de agua"),
    FALTA_SANEAMENTO("Falta saneamento"),
    SINALIZAÇAO_DANIFICADA("Sinalizaçao danificada"),
    OUTROS("Outros");

    private final String descricao;

    @com.fasterxml.jackson.annotation.JsonCreator
    public static Categoria fromString(String value){
        if(value == null || value.isBlank()) return null;
        String enums = value.trim();
        for(Categoria c : values()){
            if(c.name().equalsIgnoreCase(enums) || c.descricao.equalsIgnoreCase(enums)){
                return c;
            }
        }
        String normalized = Normalizer.normalize(enums, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase()
                .replace(" ", "_")
                .replace("-", "_");
        for(Categoria c : values()){
            if(c.name().equalsIgnoreCase(normalized)){
                return c;
            }
        }
        for(Categoria c : values()){
            if(normalized.startsWith(c.name()) || c.name().startsWith(normalized)){
                return c;
            }
        }
        return OUTROS;
    }
}
