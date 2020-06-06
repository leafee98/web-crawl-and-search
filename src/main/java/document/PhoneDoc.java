package document;

import data.CommentSummary;
import data.Phone;
import data.Price;
import lombok.Data;

@Data
public class PhoneDoc {
    private CommentSummary commentSummary;
    private Phone phone;
    private Price price;
}
