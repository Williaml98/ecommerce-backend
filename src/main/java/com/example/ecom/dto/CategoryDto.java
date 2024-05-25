/*
package com.example.ecom.dto;

import lombok.Data;

@Data
public class CategoryDto {

    private Long id;

    private String name;

    private String description;


}
*/

package com.example.ecom.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {
    private Long id;
    private String name;
    private String description;

    // Constructor for creating CategoryDto with id and name
    public CategoryDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
