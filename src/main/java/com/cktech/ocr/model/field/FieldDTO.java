@Data
@Entity
@Table(name = "field_t")
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class FieldDTO extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_code", nullable = false, length = 5)
    private String companyCode;

    @Column(name = "screen_code")
    private String screenCode;

    @Column(name = "field_code")
    private String fieldCode;

    @Column(name = "field_name")
    private String fieldName;

    @Column(name = "pattern")
    private String pattern;
}