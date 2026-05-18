package butvan.cn.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DialogMessageDTO {

    public static final String TYPE_SERVER = "server";
    public static final String TYPE_USER = "user";

    private String type;
    private String text;
    private String imageUrl;
    private String fileUrl;
    private String openUrl;
}
