package entities;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import machine.Tasks;

@Repository
public interface ProcessRepository extends PagingAndSortingRepository<Process, Integer> {

	public Process findById(Integer id);
	public Process findByName(String name);
	public Page<Process> findAll(Pageable pageable);
	public Page<Process> findByState(Pageable pageable, Tasks state);
}
