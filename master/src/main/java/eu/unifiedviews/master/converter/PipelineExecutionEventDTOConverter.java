package eu.unifiedviews.master.converter;

import java.util.ArrayList;
import java.util.List;

import cz.cuni.mff.xrg.odcs.commons.app.execution.message.MessageRecord;
import eu.unifiedviews.master.model.DPUInstanceDTO;
import eu.unifiedviews.master.model.PipelineExecutionEventDTO;

public class PipelineExecutionEventDTOConverter {

    public static PipelineExecutionEventDTO convert(MessageRecord event) {
        PipelineExecutionEventDTO dto = null;
        if (event != null) {
            dto = new PipelineExecutionEventDTO();
            dto.setId(event.getId());
            dto.setTime(ConvertUtils.dateToString(event.getTime()));
            dto.setType(event.getType());
            dto.setShortMessage(event.getShortMessage());
            dto.setFullMessage(event.getFullMessage());
            if (event.getDpuInstance() != null) {
                DPUInstanceDTO dpuInstance = new DPUInstanceDTO();
                dpuInstance.setId(event.getDpuInstance().getId());
                dpuInstance.setName(event.getDpuInstance().getName());
                dpuInstance.setDescription(event.getDpuInstance().getDescription());
                dpuInstance.setSerializedConfiguration(event.getDpuInstance().getRawConf());
                dto.setDpuInstance(dpuInstance);
            } else {
                dto.setDpuInstance(null);
            }
        }
        return dto;
    }

    public static List<PipelineExecutionEventDTO> convert(List<MessageRecord> events) {
        List<PipelineExecutionEventDTO> dtos = null;
        if (events != null) {
            dtos = new ArrayList<PipelineExecutionEventDTO>();
            for (MessageRecord event : events) {
                PipelineExecutionEventDTO dto = convert(event);
                if (dto != null) {
                    dtos.add(dto);
                }

            }
        }
        return dtos;
    }
}
