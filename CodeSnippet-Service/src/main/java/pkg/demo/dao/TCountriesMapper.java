package pkg.demo.dao;

import pkg.demo.modal.TCountries;

public interface TCountriesMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(TCountries record);

    int insertSelective(TCountries record);

    TCountries selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TCountries record);

    int updateByPrimaryKey(TCountries record);
}