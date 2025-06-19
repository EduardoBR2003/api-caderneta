package br.com.api_caderneta.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Componente responsável por converter objetos entre Entidades e DTOs.
 * Utiliza o ModelMapper configurado para realizar as conversões.
 */
@Component
public class DataMapper {

    private final ModelMapper modelMapper;

    @Autowired
    public DataMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    /**
     * Converte um objeto de origem para um tipo de destino.
     *
     * @param <O>          O tipo do objeto de origem (Entidade).
     * @param <D>          O tipo do objeto de destino (DTO).
     * @param origin       O objeto de origem a ser convertido.
     * @param destination  A classe do objeto de destino.
     * @return O objeto convertido para o tipo de destino.
     */
    public <O, D> D parseObject(O origin, Class<D> destination) {
        if (origin == null) {
            return null;
        }
        return modelMapper.map(origin, destination);
    }

    /**
     * Converte uma lista de objetos de origem para uma lista de um tipo de destino.
     *
     * @param <O>          O tipo dos objetos de origem (Entidade).
     * @param <D>          O tipo dos objetos de destino (DTO).
     * @param originList   A lista de objetos de origem a ser convertida.
     * @param destination  A classe dos objetos de destino.
     * @return A lista de objetos convertidos para o tipo de destino.
     */
    public <O, D> List<D> parseListObjects(List<O> originList, Class<D> destination) {
        if (originList == null) {
            return new ArrayList<>();
        }
        return originList.stream()
                .map(origin -> parseObject(origin, destination))
                .collect(Collectors.toList());
    }
}