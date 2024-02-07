package in.kahl.promptwhispers.model.openai;

import java.util.List;

public record DalleResponse(
        List<DalleData> data
) {
}
