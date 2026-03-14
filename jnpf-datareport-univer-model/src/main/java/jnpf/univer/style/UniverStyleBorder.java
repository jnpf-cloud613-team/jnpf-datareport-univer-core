package jnpf.univer.style;

import lombok.Data;

import java.io.Serializable;

/**
 * Style properties of top, bottom, left and right border
 * TLBR = 'tlbr', //START_TOP_LEFT_END_BOTTOM_RIGHT
 * TLBC = 'tlbc', // START_TOP_LEFT_END_BOTTOM_CENTER
 * TLMR = 'tlmr', // START_TOP_LEFT_END_MIDDLE_RIGHT
 * BLTR = 'bltr', // START_BOTTOM_LEFT_END_TOP_RIGHT
 * MLTR = 'mltr', // START_MIDDLE_LEFT_END_TOP_RIGHT
 * BCTR = 'bctr', // START_BOTTOM_CENTER_END_TOP_RIGHT
 *
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/5/11 下午4:35
 */
@Data
public class UniverStyleBorder implements Serializable {
    private UniverStyleBorderStyle t;
    private UniverStyleBorderStyle r;
    private UniverStyleBorderStyle b;
    private UniverStyleBorderStyle l;

    private UniverStyleBorderStyle tl_br;
    private UniverStyleBorderStyle tl_bc;
    private UniverStyleBorderStyle tl_mr;

    private UniverStyleBorderStyle bl_tr;
    private UniverStyleBorderStyle ml_tr;
    private UniverStyleBorderStyle bc_tr;
}
