package se325.assignment01.concert.service.mapper;

//converts Performer objects used in Performer methods into DTO objects

import se325.assignment01.concert.common.dto.PerformerDTO;
import se325.assignment01.concert.service.domain.Performer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PerformerMapper {
    public static PerformerDTO MakeDTO(Performer performer){
        return new PerformerDTO(performer.getId(), performer.getName(), performer.getImageName(), performer.getGenre(), performer.getBlurb());
    }

    public static List<PerformerDTO> MakeDTOList(List<Performer> performers) {
        List<PerformerDTO> _listDTO = new ArrayList<>();

        for (Performer _performer: performers){
            _listDTO.add(PerformerMapper.MakeDTO(_performer));
        }
        return _listDTO;
    }

    public static List<PerformerDTO> MakeDTOSet (Set<Performer> performerSet) {
        ArrayList<Performer> performerList = new ArrayList<>(performerSet);
        return PerformerMapper.MakeDTOList(performerList);
    }
}
